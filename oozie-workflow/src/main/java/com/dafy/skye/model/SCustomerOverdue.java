package com.dafy.skye.model;

import java.io.Serializable;

/**
 * Created by quanchengyun on 2017/12/7.
 */
public class SCustomerOverdue implements Serializable {

    public long customer_id;

    public int biz_type;

    public int overdue_loan_order_num;

    public int overdue_loan_amt;

    public int overdue_day;



}
