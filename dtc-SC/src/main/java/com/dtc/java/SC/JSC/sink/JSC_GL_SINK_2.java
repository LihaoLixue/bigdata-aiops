package com.dtc.java.SC.JSC.sink;

import com.dtc.java.SC.common.MySQLUtil;
import org.apache.flink.api.java.tuple.Tuple7;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;

/**
 * Created on 2019-09-12
 *
 * @author :hao.li
 */

public class JSC_GL_SINK_2 extends RichSinkFunction<Tuple7<String,String,String,String,String,String,String>> {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ParameterTool parameterTool;
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        parameterTool = (ParameterTool) (getRuntimeContext().getExecutionConfig().getGlobalJobParameters());
        // 加载JDBC驱动
        connection = MySQLUtil.getConnection(parameterTool);
        String sinkSql = "replace into SC_JSC_GLDP_ZZFW (`sjgd`, `bgsx`,`fwgd`,`gjgd`,`zysq`,`wpgd`,`qt`,`riqi`,`js_time`) VALUES " +
                "(?,?,?,?,?,?,?,?,?)";
        preparedStatement = connection.prepareStatement(sinkSql);
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (preparedStatement != null) {
            preparedStatement.close();
        }
        if (connection != null) {
            connection.close();
        }
        super.close();
    }

    @Override
    public void invoke(Tuple7<String,String,String,String,String,String,String> value, Context context) throws Exception {
        //(标志，工单，变更，等级1，2，3，4，总和,维保，废弃，健康)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String riqi = sdf.format(System.currentTimeMillis());
        String js_time = sdf1.format(System.currentTimeMillis());
        try {
            preparedStatement.setString(1, value.f0);
            preparedStatement.setString(2, value.f1);
            preparedStatement.setString(3, value.f2);
            preparedStatement.setString(4, value.f3);
            preparedStatement.setString(5, value.f4);
            preparedStatement.setString(6, value.f5);
            preparedStatement.setString(7, value.f6);
            preparedStatement.setString(8, riqi);
            preparedStatement.setString(9,js_time);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

