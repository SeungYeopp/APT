package com.ssafy.apt.service;

import java.util.List;

import com.ssafy.apt.mapper.MapMapper;
import com.ssafy.apt.dto.SidoGugunCodeDto;
import com.ssafy.apt.vo.DongCode;
import com.ssafy.apt.vo.GugunCode;
import com.ssafy.apt.vo.SidoCode;
import org.springframework.stereotype.Service;

@Service
public class MapServiceImpl implements MapService {
	
	private final MapMapper mapMapper;

	public MapServiceImpl(MapMapper mapMapper) {
		this.mapMapper = mapMapper;
	}

	@Override
	public List<SidoCode> getSido() throws Exception {
		return mapMapper.getSido();
	}

	@Override
	public List<GugunCode> getGugunInSido(String sido) throws Exception {
		return mapMapper.getGugunInSido(sido);
	}

	@Override
	public List<DongCode> getDongInGugun(String gugun) throws Exception {
		return mapMapper.getDongInGugun(gugun);
	}

}
