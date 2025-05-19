package com.ssafy.apt.service;

import com.ssafy.apt.dto.SidoGugunCodeDto;
import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.GugunCode;
import com.ssafy.apt.vo.SidoCode;

import java.sql.SQLException;
import java.util.List;

public interface MapService {

	List<SidoCode> getSido() throws Exception;
	List<GugunCode> getGugunInSido(String sido) throws Exception;
	List<DongCode> getDongInGugun(String gugun) throws Exception;
	
}
