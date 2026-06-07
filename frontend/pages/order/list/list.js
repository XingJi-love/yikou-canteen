/**
 * 订单列表页（pages/order/list/list.js）
 * 采用游标分页（last_id）拉取已支付订单，支持下拉刷新 + 上拉加载更多
 * 每项显示首件商品名、件数、价格和取餐状态，点击进入订单详情
 */
const app = getApp()
const fetch = app.fetch
Page({
  data: {
    is_last: true,  // 是否已加载完所有数据
    order: {}       // 订单列表数据
  },
  last_id: 0,      // 游标：已加载的最后一条订单 ID
  row: 10,         // 每页条数

  /** 页面加载时首次请求订单列表 */
  onLoad: function () {
    wx.showLoading({ title: '加载中' })
    this.loadData({
      last_id: 0,
      success: data => { this.setData({ order: data.list }, () => { wx.hideLoading() }) },
      fail: () => { this.onLoad() }
    })
  },

  /** 封装请求逻辑：调用 /food/orderlist（游标分页） */
  loadData: function (options) {
    wx.showNavigationBarLoading()
    fetch('/food/orderlist', { last_id: options.last_id, row: this.row }).then(data => {
      this.last_id = data.last_id
      this.setData({ is_last: data.list.length < this.row }, () => {
        wx.hideNavigationBarLoading()
        options.success(data)
      })
    }, () => { wx.hideNavigationBarLoading(); options.fail() })
  },

  /** 下拉刷新：重置游标重新加载 */
  onPullDownRefresh: function () {
    wx.showLoading({ title: '加载中' })
    this.loadData({
      last_id: 0,
      success: data => { this.setData({ order: data.list }, () => { wx.hideLoading(); wx.stopPullDownRefresh() }) },
      fail: () => { this.onLoad() }
    })
  },

  /** 上拉触底：追加加载下一页 */
  onReachBottom: function () {
    if (this.data.is_last) return
    this.loadData({
      last_id: this.last_id,
      success: data => {
        var order = this.data.order
        data.list.forEach(item => { order.push(item) })
        this.setData({ order })
      },
      fail: () => { this.onReachBottom() }
    })
  },

  /** 点击订单项跳转详情页 */
  detail: function (e) {
    var id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/order/detail/detail?order_id=' + id })
  }
})