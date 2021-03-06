package com.nj.core.fee.algo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nj.core.fee.algoParam.internal.ChannelFeePeriodCollect2CalcInternalParam;
import com.nj.core.fee.base.ExterBusinessFeeCalcParam;
import com.nj.core.fee.base.IBusinessFeeCalc;
import com.nj.core.fee.base.InsideBusinessFeeCalcParam;
import com.nj.core.fee.base.ManualParam;
import com.nj.core.fee.constant.EBusinessFeeAlgorithm;
import com.nj.core.fee.constant.EBusinessFeeAlgorithmManualParam;

/**
 * 算法 {@link EBusinessFeeAlgorithm.CHANNELFEE_PERIODCOLLECT_2} 的实现类
 */
public class ChannelFeePeriodCollect2Calc implements IBusinessFeeCalc{

	@Override
	public Map<Integer,BigDecimal> calc(ExterBusinessFeeCalcParam externalParam,
										InsideBusinessFeeCalcParam insideParam) throws Exception {
		
		// 1.验证参数对象类型是否传入正确
		ChannelFeePeriodCollect2CalcInternalParam iParam = null;
		try {
			iParam = (ChannelFeePeriodCollect2CalcInternalParam) insideParam;
		} catch (Exception e) {
			throw new Exception("传入的参数对象类型不对，应为ChannelFeePeriodCollect2CalcInternalParam类型");
		}
		
		// 2.验证参数是否合法
		externalParam.validate(this.getClass().getName());
		iParam.validate();
		
		// 3.计算每期渠道费
		// 3.1 获取放款时手动输入的渠道费总额
		BigDecimal channelFeeTotalAmount = BigDecimal.ZERO;		//渠道费总额
		List<ManualParam> manualParamList = externalParam.getManualParams();
		for (ManualParam manualParam : manualParamList) {
			if(manualParam.getCode().equals(EBusinessFeeAlgorithmManualParam.CHANNELFEE_TOTALAMOUNT.getCode())){
				channelFeeTotalAmount = manualParam.getValue();
			}
		}
		
		// 3.2得到每期的渠道费
		Map<Integer,BigDecimal> result = new HashMap<Integer, BigDecimal>();
		int calcPeriods = iParam.getAccRepayPeriod();				//待计算期数
		if(iParam.getRepayWay().contains("10101")){					//若是先息后本，则最后一期不算渠道费
			calcPeriods = calcPeriods -1;
		}
		
		BigDecimal avgChannelFee = channelFeeTotalAmount.divide(new BigDecimal(calcPeriods),2,BigDecimal.ROUND_HALF_UP);		//每期平均渠道费
		for (int i = 1; i <= calcPeriods; i++) {
			if(i == calcPeriods){		//最后一期单独处理，防止除不尽的情况
				result.put(i, channelFeeTotalAmount.subtract(avgChannelFee.multiply(new BigDecimal(calcPeriods - 1))));
			}else{
				result.put(i, avgChannelFee);
			}
		}
		
		return result;
	}

	@Override
	public List<EBusinessFeeAlgorithmManualParam> getSupportManualParam() {
		List<EBusinessFeeAlgorithmManualParam> channelFeeData = new ArrayList<EBusinessFeeAlgorithmManualParam>();
		channelFeeData.add(EBusinessFeeAlgorithmManualParam.CHANNELFEE_TOTALAMOUNT);
		return channelFeeData;
	}

}
