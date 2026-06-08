/**
 * 首页（pages/index/index.js）
 * 展示店铺品牌信息、Banner 轮播、今日推荐及功能入口（门店自取/外卖配送）
 * 数据全部通过 /api/food/index 接口从后端动态加载
 */
const app = getApp()

Page({
    data: {
        swiper: [],               // 首页轮播图 URL 列表
        recommend_img: '',        // 今日推荐菜品图片
        recommend_name: '烧味双拼饭',
        recommend_price: '68',
        recommend_desc: '',
        store_name: '一口食堂',
        store_subtitle: '港式美味 · 用心烹饪',
        store_description: '正宗港式茶餐厅，传承香港饮食文化。我们坚持使用新鲜食材，为顾客提供地道的港式美食体验。',
        store_address: '香港九龙旺角弥敦道688号',
        store_phone: '852-1234-5678',
        store_hours: '07:00 - 22:00'
    },

    /** 页面加载时请求首页数据，若登录尚未就绪则等待回调 */
    onLoad: function () {
        const that = this
        const loadData = () => {
            wx.showLoading({title: '努力加载中', mask: true})
            const fetch = app.fetch
            fetch('/food/index').then(data => {
                wx.hideLoading()
                if (!data) {
                    console.warn('首页数据为空，使用默认值')
                    return
                }
                that.setData({
                    swiper: Array.isArray(data.img_swiper) ? data.img_swiper : [],
                    recommend_img: data.recommend_img || '',
                    recommend_name: data.recommend_name || '烧味双拼饭',
                    recommend_price: data.recommend_price || '68',
                    recommend_desc: data.recommend_desc || '',
                    store_name: data.store_name || '一口食堂',
                    store_subtitle: data.store_subtitle || '港式美味 · 用心烹饪',
                    store_description: data.store_description || '正宗港式茶餐厅，传承香港饮食文化。我们坚持使用新鲜食材，为顾客提供地道的港式美食体验。',
                    store_address: data.store_address || '香港九龙旺角弥敦道688号',
                    store_phone: data.store_phone || '852-1234-5678',
                    store_hours: data.store_hours || '07:00 - 22:00'
                })
                console.log('首页数据加载成功 - 轮播图:', that.data.swiper.length, '张')
            }).catch(err => {
                wx.hideLoading()
                console.error('首页数据加载失败:', err)
                wx.showToast({title: '加载失败，请重试', icon: 'none'})
            })
        }
        // 若登录已就绪直接加载，否则注册回调等待 app.js 通知
        if (app.userLoginReady) {
            loadData()
        } else {
            app.userLoginReadyCallback = loadData
        }
    },

    /** 跳转至点餐列表页 */
    start: function () {
        wx.navigateTo({url: '/pages/list/list'})
    },

    /** 门店自取入口（暂未实现） */
    pickupTap: function () {
        wx.showToast({title: '功能开发中', icon: 'none'})
    },

    /** 外卖配送入口（暂未实现） */
    deliveryTap: function () {
        wx.showToast({title: '功能开发中', icon: 'none'})
    }
})
