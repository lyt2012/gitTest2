package com.netease.course.dao;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.netease.course.utils.Person;

public interface PersonDao {
	
	@Select("select * from person where username = #{userName}")
	public Person getPerson(String userName);
	
	@Select("select * from person where username = #{username}")
	public int getPersonId(String userName);
	
	@Select("select * from person")
	public List<Person> getPersonList();
}
