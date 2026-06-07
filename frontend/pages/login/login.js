/**
 * 登录页（pages/login/login.js）
 * 微信静默登录入口：需用户勾选协议后点击微信登录按钮
 * 调用 wx.login 获取临时 code，再通过后端 /api/user/login 完成鉴权
 */
const app = getApp()

Page({
    data: {
        agreed: false   // 用户是否已勾选"同意协议"
    },

    /** 页面加载时检查是否已有登录凭证，若已登录则直接跳首页 */
    onLoad() {
        if (wx.getStorageSync('token')) {
            wx.switchTab({url: '/pages/index/index'})
        }
    },

    /** 切换协议勾选状态 */
    toggleAgreement() {
        this.setData({agreed: !this.data.agreed})
    },

    /** 微信一键登录按钮回调：校验协议→执行登录 */
    getPhoneNumber(e) {
        if (!this.data.agreed) {
            wx.showToast({title: '请先同意协议', icon: 'none'})
            return
        }
        wx.showLoading({title: '登录中...', mask: true})
        this.doLogin()
    },

    /**
     * 核心登录流程：wx.login → 后端 /user/login
     * 成功（isLogin=true）→ 跳首页；失败 → 弹窗提示并可重试
     */
    doLogin() {
        const that = this
        wx.login({
            success(res) {
                if (res.code) {
                    app.fetch('/user/login', {method: 'GET', js_code: res.code}).then(data => {
                        wx.hideLoading()
                        if (data && data.isLogin) {
                            wx.showToast({title: '登录成功', icon: 'success'})
                            setTimeout(() => {
                                wx.switchTab({url: '/pages/index/index'})
                            }, 1500)
                        } else {
                            wx.showModal({
                                title: '登录失败', content: '请检查服务器配置或使用真实的AppID',
                                confirmText: '重试', success(modalRes) {
                                    if (modalRes.confirm) that.doLogin()
                                }
                            })
                        }
                    }).catch(() => {
                        wx.hideLoading()
                        wx.showModal({
                            title: '网络错误', content: '无法连接到服务器',
                            confirmText: '重试', success(modalRes) {
                                if (modalRes.confirm) that.doLogin()
                            }
                        })
                    })
                } else {
                    wx.hideLoading()
                    wx.showToast({title: '获取登录凭证失败', icon: 'none'})
                }
            },
            fail() {
                wx.hideLoading();
                wx.showToast({title: '微信登录失败', icon: 'none'})
            }
        })
    },

    /** 打开微信隐私协议（仅微信小程序平台支持） */
    openPrivacy() {
        wx.openPrivacyContract({
            success: () => {
            },
            fail: () => {
                wx.showToast({title: '打开协议失败', icon: 'none'})
            }
        })
    },

    /** 显示用户协议弹窗 */
    openUserAgreement() {
        wx.showModal({
            title: '用户协议',
            content: '欢迎使用一口食堂小程序。本应用旨在为用户提供便捷的食堂点餐与订餐服务。使用本应用即表示您同意我们的服务条款和隐私政策。',
            showCancel: false,
            confirmText: '我知道了'
        })
    }
})