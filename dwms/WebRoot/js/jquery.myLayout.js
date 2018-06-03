;
(function($) {
	$.extend({
		/* 给头部菜单注册点击事件，点击头部列表异步刷新内容 */
		onHeaderClick : function() {
			$("#topTree ul li ul li a").click( // 给左侧树菜单注册单击事件，这个地方可以继续优化
				function() {
					var $this = $(this);
					var $href = $this.attr("href");
					window.location.hash = $href;
					$("#topTree .active").removeClass("active");
					$this.parent("li").addClass("active");
/*					$.get($href, {"random": Math.random()}, function(data) {
						$.updatePageContent(data);
					});
*/					return false;
				});
		},
		updateByUrl: function(url) {
			if (url == '') {
				url = 'user/console';
			}
			var layerIndex = layer.load(0);
			$.get(url, {"abc": Math.random()}, function(data) {
				$.updatePageContent(data);
				layer.close(layerIndex);
				$("a[href='" + url + "']").parent().addClass("active")
				.parent().parent().addClass("open");
			});
		},
		updatePageContent: function(data) {
			if (data == "error"){
				window.location.href="index";
				return false;
			}
			layer.closeAll();
			$("#main-content-inner").html(data);
			$("#main-content-inner a[class*=_p]").click(function() {
				$href = $(this).attr("href");
				window.location.hash = $href;
				return false;
			});
			return false;
		}
	});
})(jQuery);