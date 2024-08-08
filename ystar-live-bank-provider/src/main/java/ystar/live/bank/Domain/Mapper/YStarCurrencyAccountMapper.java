package ystar.live.bank.Domain.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import ystar.live.bank.Domain.Po.YStarCurrencyAccountPO;

@Mapper
public interface YStarCurrencyAccountMapper extends BaseMapper<YStarCurrencyAccountPO> {
    
    @Update("update t_qiyu_currency_account set current_balance = current_balance + #{num} where user_id = #{userId}")
    void incr(@Param("userId") Long userId, @Param("num") int num);

    @Update("update t_qiyu_currency_account set current_balance = current_balance - #{num} where user_id = #{userId}")
    void decr(@Param("userId") Long userId, @Param("num") int num);

    @Select("select current_balance from t_qiyu_currency_account where user_id = #{userId} and status = 1")
    Integer queryBalance(Long userId);
}