package com.dafy.skye.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by quanchengyun on 2017/12/7.
 */
public class SCustomerLoan implements Serializable {

    public long customer_id;

    public int biz_type;

    public int loan_num;

    public int loan_amount;

    public int avg_loan_amt;

    public int repay_num;

    public int repay_amount;

    public int on_loan;

    public String last_loan_time;

    public String last_repay_time;

    public int repay_off_count;

    public int repay_off_days;

    public int avg_loan_duration;


}
