;
(function($) {
	$.fn.extend({ // 对于生成二级目录的树，最少需要两次遍历
		toTree : function(jsonData, right_parent_code) {
			var a = this;
			var $d_node = $("<ul class='nav nav-list'></ul>"); // 创建ul节点
			$.each(jsonData, function(i, v) { // 第一次遍历，取出一级菜单，id属性值为"o"+id
				if (v.pid == right_parent_code) { // 如果为一级菜单
					$li_node = $("<li class='me'></li>"); // 创建li节点
					
					$ul = $("<ul class='submenu'></ul>");
					$ul.attr("id", "o" + v.id); // 给li节点添加id，子菜单通过此id向此追加
					
					var $i_node = $("<a href='#' class='dropdown-toggle'><i class='menu-icon fa "+v.s_class+"'></i><span class='menu-text' style='font-size:14px'>" + v.name + "</span><b class='arrow fa fa-angle-down'></b></a>"); // 菜单内容及其链接
					/*$i_node.attr("id", "r" + v.right_code);*/ // 这个暂时没用，当初是考虑再向下分层

					$li_node.append($i_node); // 把a标签的内容添加进li标签中
					$li_node.append($ul);
					/*$li_node.append($("<b class='arrow'></b>"));*/ // 添加一个图标
					$d_node.append($li_node); // 这一步是把根菜单显示出来,下一步根据根菜单的right_code作为父right_code添加其他节点
					// alert(v['right_code']); // 这里可以取到根节点的right_code
					// 如果父节点为v['right_code'],那么把它添加到这个父节点里
					a.append($d_node);
				}
			});
			$.each(jsonData, function(i, v) { // 第二次遍历，取出二级菜单，添加到id属性值为"o"+pId的节点下
				if (v.pid != right_parent_code) {
					var $i_node = $("<li style='list-style-type:none; font-size:13px;'><a href='" + v.uri + "'><i class='" + v.s_class +"'></i>" + v.name + "</a></li>");
					$i_node.attr("id", "r" + v.id);
					$("#" + "o" + v.pid).append($i_node);
				}
			});
		}
	});
})(jQuery);