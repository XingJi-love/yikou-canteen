/**
 * 订单结算页（pages/order/checkout/checkout.js）
 * 展示订单商品明细、满减优惠、支付总价
 * 支持添加备注 → 提交支付（POST /food/order 更新备注 → POST /food/pay）
 */
const app = getApp()
const fetch = app.fetch
Page({
    data: {},
    comment: '',   // 用户输入的备注文本

    /** 根据订单 ID 加载订单详情 */
    onLoad: function (options) {
        wx.showLoading({title: '努力加载中'})
        fetch('/food/order', {id: options.order_id}).then(data => {
            this.setData(data)
            wx.hideLoading()
        }, () => {
            this.onLoad(options)
        })
    },

    /** 备注输入同步 */
    inputComment: function (e) {
        this.comment = e.detail.value
    },

    /**
     * 支付流程（两步）：
     * 1. POST /food/order 提交备注 → 2. POST /food/pay 完成支付
     * 成功后跳转订单详情页
     */
    pay: function () {
        var id = this.data.id
        wx.showLoading({title: '正在支付'})
        fetch('/food/order', {id, comment: this.comment}, 'POST')
            .then(() => fetch('/food/pay', {id}, 'POST'))
            .then(() => {
                wx.hideLoading()
                wx.showToast({
                    title: '支付成功', icon: 'success', duration: 2000, success: () => {
                        wx.navigateTo({url: '/pages/order/detail/detail?order_id=' + id})
                    }
                })
            }).catch(() => {
            this.pay()
        })
    }
})