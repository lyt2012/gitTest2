package com.netease.course.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.netease.course.utils.Trx;

public interface TrxDao {
	
	@Select("select * from trx where personId=#{personId} group by contentId, price")
	public List<Trx> getTrxListById(int personId);
	
	@Select("select * from trx")
	public List<Trx> geTrxList();
	
	@Select("select * from trx where contentId=#{contentId} group by contentId")
	public Trx geTrxByContentId(@Param("contentId") int contentId);
	
	@Insert("insert into trx(contentId,personId,price,time) values(#{contentId},#{personId},#{price},#{time})")
	public void insert(@Param("contentId") int contentId,@Param("personId") int personId,@Param("price") int price, @Param("time") long time);
}
