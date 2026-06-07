/**
 * 全局网络请求模块（utils/fetch.js）
 * 封装 wx.request，自动携带 Cookie（JSESSIONID）
 * 统一处理响应状态码和业务错误码（code === 0 视为失败）
 * 返回 Promise，失败时弹出"重试"弹窗
 */
const config = require('./config.js')
const decodeCookie = require('./decodeCookie.js')
var sess = wx.getStorageSync('JSESSIONID')

module.exports = function (path, data, method) {
    return new Promise((resolve, reject) => {
        wx.request({
            url: config.baseUrl + path,
            method: method,
            data: data,
            header: {'Cookie': sess ? 'JSESSIONID=' + sess : ''},
            success: res => {
                // 若响应头含 Set-Cookie，解析并持久化 JSESSIONID
                if (res.header['Set-Cookie'] !== undefined) {
                    sess = decodeCookie(res.header['Set-Cookie'])['JSESSIONID']
                    wx.setStorageSync('JSESSIONID', sess)
                }
                // HTTP 状态码非 200 → 服务器异常
                if (res.statusCode !== 200) {
                    fail('服务器异常', reject);
                    return
                }
                // 业务错误码 code === 0 → 请求失败，显示后端 msg
                if (res.data.code === 0) {
                    fail(res.data.msg, reject);
                    return
                }
                resolve(res.data)
            },
            fail: function () {
                fail('加载数据失败', reject)
            }
        })
    })
}

/** 错误提示弹窗（提供重试按钮，点击重试即再次调用 reject 回调） */
function fail(title, callback) {
    wx.hideLoading()
    wx.showModal({
        title, confirmText: '重试',
        success: res => {
            if (res.confirm) callback()
        }
    })
}
