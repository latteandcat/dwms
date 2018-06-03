var showSGChart ;

var sgBarOption ;

// 配置路径
require.config({
	paths : {
		echarts : "plugins/echart"
	}
});
// 图1-1,仪表盘图
require(
	[ 
	  'echarts', 
	  'echarts/chart/bar', // 使用柱状图就加载bar模块，按需加载
	  'echarts/chart/pie',
	  'echarts/chart/line',
	  'echarts/chart/funnel'
	],
	function(ec) {
		option3 = {
			    tooltip : {
			        trigger: 'axis',
			        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
			            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
			        }
			    },
			    legend: {
		            data : ['社情民意','治安防范','化解矛盾','走访群众','宣传教育','线索管理','重点人口','消防管理','交通管理','工作日志']

			    },
			    toolbox: {
			        show : true,
			        feature : {
			            magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
			            restore : {show: true},
			            saveAsImage : {show: true}
			        }
			    },
			    calculable : true,
			    xAxis : [
			        {
			            type : 'value'
			        }
			    ],
			    yAxis : [
			        {
			            type : 'category',
			            data : []
			            	//['信阳市公安局老城派出所金牛产业聚集区治安管理中队单位1','信阳市公安局老城派出所金牛产业聚集区治安管理中队单位2','信阳市公安局老城派出所金牛产业聚集区治安管理中队单位3','单位4','单位5','单位6','单位7','单位8','单位9','单位10','单位11','单位12','单位13','单位14','单位15','单位16','单位17','单位18','单位19','单位20']
			        }
			    ],
			    grid: {
			    	x:'30%' , y:'7%' , width: '68%', height: '85%'
			    },
			    series : [
			        { 
			            name:'社情民意',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[320, 302, 301, 334, 390, 330, 320]
			        },
			        {
			            name:'治安防范',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[120, 132, 101, 134, 90, 230, 210]
			        },
			        {
			            name:'化解矛盾',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[220, 182, 191, 234, 290, 330, 310]
			        },
			        {
			            name:'走访群众',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[150, 212, 201, 154, 190, 330, 410]
			        },
			        {
			            name:'宣传教育',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[820, 832, 901, 934, 1290, 1330, 1320]
			        },
			        {
			            name:'线索管理',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[1150, 212, 201, 154, 190, 330, 410]
			        }
			        ,
			        {
			            name:'重点人口',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[1250, 212, 201, 154, 190, 330, 410]
			        }
			        ,
			        {
			            name:'消防管理',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[1540, 212, 201, 154, 190, 330, 410]
			        }
			        ,
			        {
			            name:'交通管理',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[140, 212, 201, 154, 190, 330, 410]
			        }
			        ,
			        {
			            name:'工作日志',
			            type:'bar',
			            stack: '总量',
			            itemStyle : { normal: {label : {show: true, position: 'insideRight'}}},
			            data:[]
			            	//[140, 212, 201, 154, 190, 330, 410]
			        }
			    ]
			};
			                    
	// 基于准备好的dom，初始化echarts图表
	showSGBar = function(startTime,endTime,policename,policeid,dwid) {
		/*各种类型的数据量*/
	 	$.post("statis/list",{startTime:startTime,endTime:endTime,policename:policename,policeid:policeid,dwid:dwid},function(data){
			//根据单位列表长度，确定页面中div高度300px起价
		var t = data.dwmcData.length;
		var h = 300+(t-1)*20;
		$("#sgBarDiv").attr("style","height:"+h+"px;");
		 
		option3.yAxis[0].data = data.dwmcData;
		option3.series[0].data = data.data.sg_polls;
		option3.series[1].data = data.data.sg_prevention;
		option3.series[2].data = data.data.sg_dispute;
		option3.series[3].data = data.data.sg_investigate;
		option3.series[4].data = data.data.sg_propagate;
		option3.series[5].data = data.data.sg_clue;
		option3.series[6].data = data.data.sg_emphasis_person;
		option3.series[7].data = data.data.sg_fire;
		option3.series[8].data = data.data.sg_traffic;
		option3.series[9].data = data.data.sg_work_log;
		
			//['信阳市公安局老城派出所金牛产业聚集区治安管理中队单位1','信阳市公安局老城派出所金牛产业聚集区治安管理中队单位2','信阳市公安局老城派出所金牛产业聚集区治安管理中队单位3','单位4','单位5','单位6','单位7','单位8','单位9','单位10','单位11','单位12','单位13','单位14','单位15','单位16','单位17','单位18','单位19','单位21'];
		
		myChart1_5 = ec.init(document.getElementById('sgBarDiv')); // 柱状图
		myChart1_5.setOption(option3);
	 });
		
	}
	// 基于准备好的dom，初始化echarts图表
	showSGBar2 = function(startTime,endTime,policename,policeid,dwid) {
		/*各种类型的数据量*/
	 	$.post("score/list",{startTime:startTime,endTime:endTime,policename:policename,policeid:policeid,dwid:dwid},function(data){
			//根据单位列表长度，确定页面中div高度300px起价
		var t = data.dwmcData.length;
		var h = 300+(t-1)*20;
		$("#sgBarDiv2").attr("style","height:"+h+"px;");
		 
		option3.yAxis[0].data = data.dwmcData;
		option3.series[0].data = data.data.sg_polls;
		option3.series[1].data = data.data.sg_prevention;
		option3.series[2].data = data.data.sg_dispute;
		option3.series[3].data = data.data.sg_investigate;
		option3.series[4].data = data.data.sg_propagate;
		option3.series[5].data = data.data.sg_clue;
		option3.series[6].data = data.data.sg_emphasis_person;
		option3.series[7].data = data.data.sg_fire;
		option3.series[8].data = data.data.sg_traffic;
		option3.series[9].data = data.data.sg_work_log;
		
			//['信阳市公安局老城派出所金牛产业聚集区治安管理中队单位1','信阳市公安局老城派出所金牛产业聚集区治安管理中队单位2','信阳市公安局老城派出所金牛产业聚集区治安管理中队单位3','单位4','单位5','单位6','单位7','单位8','单位9','单位10','单位11','单位12','单位13','单位14','单位15','单位16','单位17','单位18','单位19','单位21'];
		
		myChart1_6 = ec.init(document.getElementById('sgBarDiv2')); // 柱状图
		myChart1_6.setOption(option3);
	 });
		
	}
});