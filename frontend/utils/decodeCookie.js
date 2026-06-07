/**
 * Cookie 解析工具（utils/decodeCookie.js）
 * 将 Set-Cookie 字符串解析为键值对对象
 * 支持多 Cookie（逗号分隔）和 Cookie 属性（分号分隔）
 */
module.exports = function (cookie) {
  var obj = {}
  cookie.split(',').forEach((item) => {
    item.split('; ').forEach((item) => {
      var arr = item.split('=')
      obj[arr[0]] = arr[1] !== undefined ? decodeURIComponent(arr[1]) : true
    })
  })
  return obj
}
