package com.teamwork.contract;

/**
 * Hợp đồng bắt buộc mọi Plugin phải tuân thủ để được Core Framework nạp và quản
 * lý.
 */
public interface IPlugin {
    String getName();

    void initialize(IHostContext context);

    void start();

    void stop();
}