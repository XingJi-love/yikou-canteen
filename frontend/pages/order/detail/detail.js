/**
 * 订单详情页（pages/order/detail/detail.js）
 * 展示取餐码、订单明细、满减优惠、支付信息与时间线
 * 页面卸载时自动返回订单列表页
 */
const app = getApp()
const fetch = app.fetch
Page({
  data: {},

  /** 根据订单 ID 获取完整订单数据 */
  onLoad: function (options) {
    var id = options.order_id
    wx.showLoading({ title: '努力加载中' })
    fetch('/food/order', { id }).then(data => {
      this.setData(data)
      wx.hideLoading()
    }, () => { this.onLoad(options) })
  },

  /** 页面卸载时返回订单列表（reLaunch 清空页面栈） */
  onUnload: function () {
    wx.reLaunch({ url: '/pages/order/list/list' })
  }
})
