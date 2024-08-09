package ystar.live.bank.service;

import ystar.live.bank.Domain.Po.PayTopicPO;

public interface IPayTopicService {

    PayTopicPO getByCode(Integer code);
}