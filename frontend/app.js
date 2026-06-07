/**
 * 小程序全局入口文件 app.js
 * 负责应用生命周期管理和全局登录状态控制
 * - 启动时自动检测登录状态（Cookie/JSESSIONID）
 * - 提供全局 login 方法供各页面调用
 * - 通过 userLoginReady 机制协调页面加载顺序（避免 login 回调前页面已渲染）
 */
App({
    /** 挂载全局网络请求模块，所有页面统一通过 app.fetch 调用 */
    fetch: require('./utils/fetch.js'),

    /** 小程序初始化时自动执行：验证登录状态 */
    onLaunch: function () {
        // 获取当前页面栈，判断是否在登录页
        const pages = getCurrentPages()
        const currentPage = pages[pages.length - 1]

        // 若当前在登录页面，跳过自动登录，直接标记就绪
        if (currentPage && currentPage.route && currentPage.route.includes('login')) {
            console.log('当前在登录页面，跳过自动登录')
            this.userLoginReady = true
            return
        }

        // 无本地 JSESSIONID → 未登录，直接标记就绪
        if (!wx.getStorageSync('JSESSIONID')) {
            console.log('未登录，标记为就绪状态')
            this.userLoginReady = true
            return
        }

        // 已有本地 Cookie，向服务器验证其是否仍然有效
        wx.showLoading({title: '登录中', mask: true})
        this.fetch('/user/checkLogin').then(data => {
            if (data.isLogin) {
                // Cookie 有效 → 恢复登录状态
                this.onUserLoginReady()
                console.log('通过保存的Cookie登录成功')
            } else {
                wx.hideLoading()
                // Cookie 已过期 → 清除本地存储，标记就绪
                wx.removeStorageSync('JSESSIONID')
                this.userLoginReady = true
                console.log('Cookie已失效，已清除')
            }
        }, () => {
            wx.hideLoading()
            // 网络错误等异常情况 → 不阻塞页面，标记就绪
            this.userLoginReady = true
            console.log('登录状态检查失败')
        })
    },

    /**
     * 微信静默登录方法
     * 调用 wx.login 获取临时 code，传给后端换取 openid
     * @param {Object} options - { success: Function, fail: Function }
     */
    login: function (options) {
        wx.login({
            success: res => {
                this.fetch('/user/login', {
                    js_code: res.code
                }).then(data => {
                    if (data && data.isLogin) {
                        options.success()
                    } else {
                        wx.hideLoading()
                        wx.showModal({
                            title: '登录失败（请使用真实的AppID，并检查服务器端配置）',
                            confirmText: '重试',
                            success: res => {
                                if (res.confirm) options.fail()
                            }
                        })
                    }
                }, () => {
                    options.fail()
                })
            }
        })
    },

    /** 登录就绪标志（true 表示为已处理完登录逻辑，页面可以正常加载数据） */
    userLoginReady: false,
    /** 等待登录就绪的回调函数（在 onLaunch 中赋值，由 onUserLoginReady 触发） */
    userLoginReadyCallback: null,

    /** 登录就绪回调：隐藏 loading，执行等待中的页面回调，设置标志位 */
    onUserLoginReady: function () {
        wx.hideLoading()
        if (this.userLoginReadyCallback) {
            this.userLoginReadyCallback()
        }
        this.userLoginReady = true
    }
})