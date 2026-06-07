/**
 * 点餐列表页（pages/list/list.js）
 * 左侧分类导航 + 右侧菜品列表（联动滚动）
 * 购物车管理（加入/增减/清空）、贝塞尔曲线抛物动画、满减优惠计算、提交订单
 */
const shopcartAnimate = require('../../utils/shopcartAnimate.js')
const app = getApp()
const fetch = app.fetch
/** 右侧各分类区间的顶部偏移量数组（用于联动高亮计算） */
const categoryPosition = []

Page({
  data: {
    foodList: [],       // 分类菜品列表 [{ name, food: [...] }]
    promotion: {},      // 满减优惠规则
    activeIndex: 0,     // 左侧分类高亮索引
    tapIndex: 0,        // 上次手动点击的分类索引（用于 scroll-into-view）
    cartPrice: 0,       // 购物车总价
    cartNumber: 0,      // 购物车总件数
    cartList: {},       // 购物车明细 { index: { id, name, price, number } }
    showCart: false,    // 是否展开购物车浮层
  },
  disableNextScroll: false,  // 防止手动点击分类后 scroll 事件覆盖 activeIndex
  shopcartAnimate: null,

  onLoad: function () {
    wx.showLoading({ title: '努力加载中' })
    fetch('/food/list').then(data => {
      wx.hideLoading()
      this.setData({ foodList: data.list, promotion: data.promotion[0] }, () => {
        // 计算各分类在右侧 scroll-view 中的位置，供联动高亮使用
        var query = wx.createSelectorQuery()
        var top = 0, height = 0
        query.select('.food').boundingClientRect(rect => { top = rect.top; height = rect.height })
        query.selectAll('.food-category').boundingClientRect(res => {
          res.forEach(rect => { categoryPosition.push(rect.top - top - height / 3) })
        })
        query.exec()
      })
    }, () => { this.onLoad() })
    // 初始化购物车贝塞尔抛物动画实例
    this.shopcartAnimate = shopcartAnimate('.operate-shopcart-icon', this)
  },

  /** 左侧分类点击：高亮对应分类并滚动右侧到对应位置 */
  tapCategory: function (e) {
    this.disableNextScroll = true
    var index = e.currentTarget.dataset.index
    this.setData({ activeIndex: index, tapIndex: index })
  },

  /** 右侧列表滚动时实时高亮左侧分类 */
  onFoodScroll: function (e) {
    if (this.disableNextScroll) { this.disableNextScroll = false; return }
    var scrollTop = e.detail.scrollTop
    var activeIndex = 0
    categoryPosition.forEach((item, i) => { if (scrollTop >= item) activeIndex = i })
    if (activeIndex !== this.data.activeIndex) { this.setData({ activeIndex }) }
  },

  /** 加入购物车（触发抛物动画） */
  addToCart: function (e) {
    const index = e.currentTarget.dataset.index
    const category_index = e.currentTarget.dataset.category_index
    const food = this.data.foodList[category_index].food[index]
    const cartList = this.data.cartList
    if (cartList[index]) { ++cartList[index].number }
    else {
      cartList[index] = { id: food.id, name: food.name, price: parseFloat(food.price), number: 1 }
    }
    this.setData({ cartList, cartPrice: this.data.cartPrice + cartList[index].price, cartNumber: this.data.cartNumber + 1 })
    this.shopcartAnimate.start(e)
  },

  /** 切换购物车浮层显示/隐藏（仅购物车非空时可操作） */
  showCartList: function () {
    if (this.data.cartNumber > 0) { this.setData({ showCart: !this.data.showCart }) }
  },

  /** 购物车内数量 +1 */
  cartNumberAdd: function(e) {
    var id = e.currentTarget.dataset.id
    var cartList = this.data.cartList
    ++cartList[id].number
    this.setData({ cartList, cartNumber: ++this.data.cartNumber, cartPrice: this.data.cartPrice + cartList[id].price })
  },

  /** 购物车内数量 -1（减到 0 则删除该项） */
  cartNumberDec: function(e) {
    var id = e.currentTarget.dataset.id
    var cartList = this.data.cartList
    if (cartList[id]) {
      var price = cartList[id].price
      if (cartList[id].number > 1) { --cartList[id].number }
      else { delete cartList[id] }
      this.setData({ cartList, cartNumber: --this.data.cartNumber, cartPrice: this.data.cartPrice - price })
      if (this.data.cartNumber <= 0) { this.setData({ showCart: false }) }
    }
  },

  /** 清空购物车 */
  cartClear: function() {
    this.setData({ cartList: {}, cartNumber: 0, cartPrice: 0, showCart: false })
  },

  /**
   * 提交购物车生成订单
   * POST /food/order 传入 cartList，成功后跳转结算页
   */
  order: function() {
    if (this.data.cartNumber === 0) return
    wx.showLoading({ title: '正在生成订单' })
    fetch('/food/order', { order: this.data.cartList }, 'POST').then(data => {
      wx.navigateTo({ url: '/pages/order/checkout/checkout?order_id=' + data.order_id })
    }, () => { this.order() })
  }
})