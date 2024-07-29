package com.ystar.id.generate.provider.Domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName ystar_id_generate_config
 */
@TableName(value ="ystar_id_generate_config")
public class IdGeneratePo implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 备注
     */
    private String remark;

    /**
     * 当前id所在阶段的阈值
     */
    private Long nextThreshold;

    /**
     * 初始化值
     */
    private Long initNum;

    /**
     * 当前id所在阶段的开始值
     */
    private Long currentStart;

    /**
     * id递增区间
     */
    private Integer step;

    /**
     * 是否有序(0无序，1有序)
     */
    private Integer isSeq;

    /**
     * id前缀
     */
    private String idPrefix;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    public Integer getId() {
        return id;
    }

    /**
     * 主键id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 当前id所在阶段的阈值
     */
    public Long getNextThreshold() {
        return nextThreshold;
    }

    /**
     * 当前id所在阶段的阈值
     */
    public void setNextThreshold(Long nextThreshold) {
        this.nextThreshold = nextThreshold;
    }

    /**
     * 初始化值
     */
    public Long getInitNum() {
        return initNum;
    }

    /**
     * 初始化值
     */
    public void setInitNum(Long initNum) {
        this.initNum = initNum;
    }

    /**
     * 当前id所在阶段的开始值
     */
    public Long getCurrentStart() {
        return currentStart;
    }

    /**
     * 当前id所在阶段的开始值
     */
    public void setCurrentStart(Long currentStart) {
        this.currentStart = currentStart;
    }

    /**
     * id递增区间
     */
    public Integer getStep() {
        return step;
    }

    /**
     * id递增区间
     */
    public void setStep(Integer step) {
        this.step = step;
    }

    /**
     * 是否有序(0无序，1有序)
     */
    public Integer getIsSeq() {
        return isSeq;
    }

    /**
     * 是否有序(0无序，1有序)
     */
    public void setIsSeq(Integer isSeq) {
        this.isSeq = isSeq;
    }

    /**
     * id前缀
     */
    public String getIdPrefix() {
        return idPrefix;
    }

    /**
     * id前缀
     */
    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    /**
     * 乐观锁版本号
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * 乐观锁版本号
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}