/**
 * 个人中心页（pages/record/record.js）
 * 展示用户头像/昵称、累计消费统计、会员等级
 * 提供编辑资料、订单、联系商家、清理缓存、检查更新、退出登录等功能
 * 并展示历史消费记录列表
 */
const defaultAvatar = '/images/avatar.png'
const app = getApp()
const fetch = app.fetch

Page({
    data: {
        defaultAvatar,
        avatarUrl: defaultAvatar,
        userInfo: {},       // 用户个人信息
        list: [],           // 消费记录列表
        totalSpend: '0.00',
        memberLevel: '普通会员',

        // 个人信息编辑弹窗
        showPopup: false,
        submitting: false,
        editForm: {avatarUrl: '', nickname: '', phone: '', gender: '', genderIndex: 0},
        genderOptions: ['未知', '男', '女']
    },

    onLoad: function () {
        this.loadUserData()
    },
    onShow: function () {
        this.loadUserData()
    },
    onPullDownRefresh: function () {
        this.loadUserData().then(() => {
            wx.stopPullDownRefresh()
        })
    },

    /**
     * 并行请求用户信息 /user/profile 和消费记录 /food/record
     * 根据累计消费金额自动评定会员等级
     * 首次登录（昵称为空）自动弹出编辑弹窗
     */
    loadUserData: async function () {
        wx.showLoading({title: '加载中'})
        try {
            const [profileRes, recordRes] = await Promise.all([
                fetch('/user/profile').catch(() => null),
                fetch('/food/record')
            ])
            if (profileRes && profileRes.id) {
                let level = '普通会员'
                const price = parseFloat(profileRes.price) || 0
                if (price >= 2000) level = '钻石会员'
                else if (price >= 1000) level = '金牌会员'
                else if (price >= 300) level = '银牌会员'
                this.setData({
                    userInfo: profileRes,
                    avatarUrl: profileRes.avatarUrl || defaultAvatar,
                    memberLevel: level
                })
                // 昵称为空 → 首次登录，自动弹出编辑弹窗
                if (!profileRes.nickname || !profileRes.nickname.trim()) {
                    setTimeout(() => this.showEditPopup(), 500)
                }
            }
            if (recordRes) {
                const list = recordRes.list || []
                const total = list.reduce((sum, item) => sum + (parseFloat(item.price) || 0), 0)
                this.setData({list, totalSpend: total.toFixed(2)})
            }
        } catch (e) {
            console.error('加载数据失败:', e)
        } finally {
            wx.hideLoading()
        }
    },

    /** 选择头像（微信 chooseAvatar 回调） */
    onChooseAvatar: function (e) {
        const {avatarUrl} = e.detail
        this.setData({avatarUrl, 'editForm.avatarUrl': avatarUrl})
    },

    /** 显示编辑个人资料弹窗（填充当前用户数据） */
    showEditPopup: function () {
        const {userInfo, genderOptions} = this.data
        const genderIdx = Math.max(0, genderOptions.indexOf(['未知', '男', '女'][userInfo.gender] ?? '未知'))
        this.setData({
            showPopup: true,
            editForm: {
                avatarUrl: userInfo.avatarUrl || '',
                nickname: userInfo.nickname || '',
                phone: userInfo.phone || '',
                gender: userInfo.gender ?? 0,
                genderIndex: genderIdx
            }
        })
    },

    /** 隐藏编辑弹窗 */
    hideEditPopup: function () {
        this.setData({showPopup: false})
    },

    /** 阻止弹窗内滚动穿透 */
    preventMove: function () {
        return false
    },

    /** 昵称输入同步 */
    onNicknameInput: function (e) {
        this.setData({'editForm.nickname': e.detail.value})
    },
    /** 手机号输入同步 */
    onPhoneInput: function (e) {
        this.setData({'editForm.phone': e.detail.value})
    },
    /** 性别选择同步 */
    onGenderChange: function (e) {
        const idx = parseInt(e.detail.value)
        this.setData({'editForm.genderIndex': idx, 'editForm.gender': idx})
    },

    /**
     * 提交保存个人资料（PUT /user/profile）
     * 校验昵称非空、手机号格式（可选）
     */
    submitProfile: function () {
        const {editForm} = this.data
        const {nickname, phone} = editForm
        if (!nickname.trim()) return wx.showToast({title: '请输入昵称', icon: 'none'})
        if (phone && !/^1\d{10}$/.test(phone)) return wx.showToast({title: '请输入正确的手机号', icon: 'none'})
        this.setData({submitting: true})
        fetch('/user/profile', {
            nickname: nickname.trim(),
            avatarUrl: editForm.avatarUrl || '',
            phone: phone.trim(),
            gender: editForm.gender
        }, 'PUT').then(() => {
            wx.showToast({title: '保存成功', icon: 'success'})
            this.setData({showPopup: false})
            this.loadUserData()
        }).catch((err) => {
            wx.showToast({title: err.message || '保存失败', icon: 'none'})
        }).finally(() => {
            this.setData({submitting: false})
        })
    },

    /** 跳转至我的订单 */
    navigateToOrder: function () {
        wx.switchTab({url: '/pages/order/list/list'})
    },

    /** 联系商家：直接拨号，失败则复制号码到剪贴板 */
    contactUs: function () {
        wx.makePhoneCall({phoneNumber: '852-1234-5678'}).catch(() => {
            wx.setClipboardData({
                data: '852-1234-5678',
                success: () => wx.showToast({title: '号码已复制', icon: 'success'})
            })
        })
    },

    /** 关于我们 */
    aboutUs: function () {
        wx.showModal({
            title: '一口食堂',
            content: '正宗港式茶餐厅\n传承香港饮食文化\n用心烹饪每一道菜品\n\nVersion 1.0.0',
            confirmText: '我知道了',
            showCancel: false
        })
    },

    /** 清除本地存储 */
    clearCache: function () {
        wx.showModal({
            title: '提示', content: '确定要清除缓存吗？', confirmColor: '#F7982A',
            success: (res) => {
                if (res.confirm) {
                    wx.clearStorageSync();
                    wx.showToast({title: '缓存已清除', icon: 'success'})
                }
            }
        })
    },

    /** 检查小程序更新（使用 UpdateManager） */
    checkUpdate: function () {
        const updateManager = wx.getUpdateManager()
        updateManager.onCheckForUpdate((res) => {
            if (res.hasUpdate) {
                updateManager.onUpdateReady(() => {
                    wx.showModal({
                        title: '更新提示', content: '新版本已准备好，是否重启应用？', confirmColor: '#F7982A',
                        success: (res) => {
                            if (res.confirm) updateManager.applyUpdate()
                        }
                    })
                })
                updateManager.onUpdateFailed(() => {
                    wx.showToast({title: '更新失败', icon: 'none'})
                })
            } else {
                wx.showToast({title: '已是最新版本', icon: 'none'})
            }
        })
    },

    /** 退出登录：清空本地存储并重启到首页 */
    logout: function () {
        wx.showModal({
            title: '退出登录', content: '确定要退出当前账号吗？', confirmColor: '#FA5151',
            success: (res) => {
                if (res.confirm) {
                    wx.clearStorageSync();
                    wx.reLaunch({url: '/pages/index/index'})
                }
            }
        })
    }
})