package com.ssafy.apt.mapper;

import com.ssafy.apt.dto.SidoGugunCodeDto;
import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.GugunCode;
import com.ssafy.apt.vo.SidoCode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Mapper
@Repository
public interface MapMapper {

	List<SidoCode> getSido() throws SQLException;
	List<GugunCode> getGugunInSido(String sido) throws SQLException;
	List<DongCode> getDongInGugun(String gugun) throws SQLException;
	
}
