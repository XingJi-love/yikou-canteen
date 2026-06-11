/**
 * 菜品详情页（pages/detail/detail.js）
 * 展示菜品完整信息：大图、名称价格、详细描述
 * 支持加入购物车功能（与列表页联动）
 */
const app = getApp()
const fetch = app.fetch

Page({
  data: {
    food: {},          // 菜品详情数据
    loading: true      // 加载状态
  },

  /**
   * 页面加载：接收 id 参数，请求菜品详情 API
   */
  onLoad: function (options) {
    if (!options.id) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.loadFoodDetail(options.id)
  },

  /** 
   * 请求菜品详情数据 
   */
  loadFoodDetail: function (id) {
    wx.showLoading({ title: '加载中' })
    fetch('/food/detail?id=' + id).then(data => {
      wx.hideLoading()
      this.setData({ food: data, loading: false })
    }, () => {
      wx.hideLoading()
      this.loadFoodDetail(id) // 失败重试
    })
  },

  /** 
   * 返回上一页 
   */
  goBack: function () {
    wx.navigateBack()
  },

  /**
   * 加入购物车：获取列表页实例并调用其 addToCart 方法
   * 实现与列表页购物车状态联动
   */
  addToCart: function () {
    const pages = getCurrentPages()
    const listPage = pages.find(p => p.route && p.route.includes('list'))
    
    if (listPage && listPage.addToCart) {
      // 构造模拟事件对象，传递当前菜品的 category_index 和 index
      // 由于详情页不知道在列表中的位置，我们直接操作 cartList 数据
      const food = this.data.food
      const cartList = listPage.data.cartList
      
      // 使用一个唯一 key 存储到 cartList
      const cartKey = 'detail_' + food.id
      if (cartList[cartKey]) {
        ++cartList[cartKey].number
      } else {
        cartList[cartKey] = { id: food.id, name: food.name, price: parseFloat(food.price), number: 1 }
      }
      
      listPage.setData({
        cartList,
        cartPrice: listPage.data.cartPrice + parseFloat(food.price),
        cartNumber: listPage.data.cartNumber + 1
      })
      
      wx.showToast({ title: '已加入购物车', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 800)
    } else {
      // 如果没有列表页实例，提示用户
      wx.showToast({ title: '请从菜单页添加', icon: 'none' })
    }
  },

  /**
   * 分享功能（可选）
   */
  onShareAppMessage: function () {
    const food = this.data.food
    return {
      title: food.name + ' - 一口食堂',
      path: '/pages/detail/detail?id=' + food.id,
      imageUrl: food.image_url
    }
  }
})
