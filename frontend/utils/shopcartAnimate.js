/**
 * 购物车抛物动画模块（utils/shopcartAnimate.js）
 * 点击"加入购物车"时，从手指位置到购物车图标绘制一条贝塞尔曲线路径
 * 通过定时器逐帧更新小球位置，产生抛物线掉落动画效果
 *
 * @param {string} iconClass  购物车图标的 CSS 选择器
 * @param {Page}   page       当前页面实例（用于 setData）
 * @returns {{ start: Function }}
 */
module.exports = function (iconClass, page) {
  // 购物车图标位置（终点）
  var busPos = {}
  wx.createSelectorQuery().select(iconClass).boundingClientRect(rect => {
    busPos.x = rect.left + 15
    busPos.y = rect.top
  }).exec()

  return {
    /**
     * 启动动画
     * @param {Event} e 触摸事件对象（从中获取手指坐标作为起点）
     */
    start: function (e) {
      // 手指位置（起点），偏移 10px 使小球中心对齐触摸点
      var finger = { x: e.touches[0].clientX - 10, y: e.touches[0].clientY - 10 }
      // 贝塞尔曲线控制点（在起点与终点之间取一个高点作为抛物线顶点）
      var topPoint = {}
      if (finger.y < busPos.y) { topPoint.y = finger.y - 150 }
      else { topPoint.y = busPos.y - 150 }
      topPoint.x = Math.abs(finger.x - busPos.x) / 2
      if (finger.x > busPos.x) { topPoint.x = (finger.x - busPos.x) / 2 + busPos.x }
      else { topPoint.x = (busPos.x - finger.x) / 2 + finger.x }

      // 计算 30 个插值点
      var bezier_points = bezier([busPos, topPoint, finger], 30).bezier_points
      page.setData({ 'cartBall.show': true, 'cartBall.x': finger.x, 'cartBall.y': finger.y })

      // 从终点向起点逐帧回溯显示小球（每 50ms 移动 5 个插值点）
      let i = bezier_points.length - 1
      var timer = setInterval(function () {
        i = i - 5
        if (i < 1) { clearInterval(timer); page.setData({ 'cartBall.show': false }); return }
        page.setData({ 'cartBall.show': true, 'cartBall.x': bezier_points[i].x, 'cartBall.y': bezier_points[i].y })
      }, 50)
    }
  }

  /**
   * 二次贝塞尔曲线插值计算
   * @param {Array} pots  三个控制点 [终点, 顶点, 起点]
   * @param {number} amount 插值点数量
   * @returns {{ bezier_points: Array }}
   */
  function bezier(pots, amount) {
    var ret = []
    for (var i = 0; i <= amount; ++i) {
      var points = pots.slice(0)
      var lines = []
      var pot
      while (pot = points.shift()) {
        if (points.length) { lines.push(pointLine([pot, points[0]], i / amount)) }
        else if (lines.length > 1) { points = lines; lines = [] }
        else break
      }
      ret.push(lines[0])
    }

    /** 两点之间按比例取点 */
    function pointLine(points, rate) {
      var pointA = points[0], pointB = points[1]
      var xDistance = pointB.x - pointA.x
      var yDistance = pointB.y - pointA.y
      var pointDistance = Math.pow(Math.pow(xDistance, 2) + Math.pow(yDistance, 2), 1 / 2)
      var radian = Math.atan(yDistance / xDistance)
      var tmpPointDistance = pointDistance * rate
      return {
        x: pointA.x + tmpPointDistance * Math.cos(radian),
        y: pointA.y + tmpPointDistance * Math.sin(radian)
      }
    }
    return { bezier_points: ret }
  }
}