package com.pulan.hystric;

import org.springframework.stereotype.Component;

import com.pulan.service.SchedualServiceHi;
@Component
public class SchedualServiceHiHystric implements SchedualServiceHi{

	@Override
	public String sayHiFromClientOne(String name) {
		// TODO Auto-generated method stub
		return "sorry"+name;
	}

}
