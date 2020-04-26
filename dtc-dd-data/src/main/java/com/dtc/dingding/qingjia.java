package com.dtc.dingding;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiProcessinstanceGetRequest;
import com.dingtalk.api.request.OapiProcessinstanceListidsRequest;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.dingtalk.api.response.OapiProcessinstanceListidsResponse;
import com.dtc.dingding.Model.JQModel;
import com.google.gson.Gson;
import com.taobao.api.ApiException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2020-02-28
 *
 * @author :hao.li
 */
public class qingjia {
    public static void main(String[] args) throws ApiException {
   // public static void info() throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/listids");
        OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
        req.setProcessCode("PROC-EF6YNNYRN2-41ZEFE73TL17CARUWAXW1-H0UO8GUI-WL");
        req.setStartTime(1583276849000L);
        req.setEndTime(1583723249000L);
        OapiProcessinstanceListidsResponse response = client.execute(req, "7c61e56799d93ff5833c0bc2a2cfbe48");
        String str = response.getBody();
//        System.out.println(str);

        JSONObject jsonObject = JSONObject.parseObject(str);
        JSONArray result = jsonObject.getJSONObject("result").getJSONArray("list");
        for (int i = 0; i < result.size(); i++) {
            String str1 = result.getString(i);
            DingTalkClient client1 = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/get");
            OapiProcessinstanceGetRequest request = new OapiProcessinstanceGetRequest();
            request.setProcessInstanceId(str1);
            OapiProcessinstanceGetResponse response1 = client1.execute(request, "7c61e56799d93ff5833c0bc2a2cfbe48");
            String body = response1.getBody();
            System.out.println(body);
            JSONObject jsonObject1 = JSONObject.parseObject(body);
            String status = jsonObject1.getJSONObject("process_instance").getString("status");
            String task_result = jsonObject1.getJSONObject("process_instance").getString("result");
            Map<String, String> map = new HashMap<>();
            if ("COMPLETED".equals(status) && "agree".equals(task_result)) {
                JSONArray operation_records = jsonObject1.getJSONObject("process_instance").getJSONArray("operation_records");
                for (int j = 0; j < operation_records.size(); j++) {
                    JSONObject jsonObject2 = operation_records.getJSONObject(j);
                    String operation_type = jsonObject2.getString("operation_type");
                    if ("START_PROCESS_INSTANCE".equals(operation_type)) {
                        String userid = jsonObject2.getString("userid");
                        map.put("userid", userid);
                        JSONArray form_component_values = jsonObject1.getJSONObject("process_instance").getJSONArray("form_component_values");
                        for (int m = 0; m < form_component_values.size(); m++) {
                            JSONObject jsonObject3 = form_component_values.getJSONObject(m);
                            if ("请假天数".equals(jsonObject3.getString("name"))) {
                                map.put("qingjia_time", jsonObject3.getString("value"));
                            }
                            if ("开始时间-结束时间".equals(jsonObject3.getString("id"))) {
                                String value = jsonObject3.getString("value");
                                if (value.startsWith("[") && value.endsWith("]")) {
                                    String[] split = value.split("\\[")[1].split("]");
                                    String start_time = split[0].split(",")[0].replace("\"", "");
                                    String end_time = split[0].split(",")[1].replace("\"", "");
                                    map.put("start_time", start_time);
                                    map.put("end_time", end_time);
                                }
                            }
                        }
                    }
                }
                String jsonString = JSON.toJSONString(map);
                Gson gson = new Gson();
                JQModel model = gson.fromJson(jsonString, JQModel.class);
                String Url = "jdbc:mysql://10.3.7.231:3306/real_time_alarm_data";//参数参考MySql连接数据库常用参数及代码示例
                String name = "root";//数据库用户名
                String psd = "DTCserver2019!";//数据库密码
                String jdbcName = "com.mysql.jdbc.Driver";//连接MySql数据库
                String sql = "replace into qingjia (" + "user_id," + "duration," + "start_time," + "end_time," + "riqi"+") values(?,?,?,?,?)";//数据库操作语句（插入）
                Connection con = null;
                String time = String.valueOf(System.currentTimeMillis());
                String riqi = timeStamp2Date(time, "");
                try {
                    Class.forName(jdbcName);//向DriverManager注册自己
                    con = DriverManager.getConnection(Url, name, psd);//与数据库建立连接
                    PreparedStatement pst = con.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                    pst.setString(1, model.getUserid());
                    pst.setString(2, model.getTime());
                    pst.setString(3, model.getStarttime());
                    pst.setString(4, model.getEndtime());
                    pst.setString(5,riqi);
                    pst.executeUpdate();//解释在下
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyMMdd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds)));
    }
}
