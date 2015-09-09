/**
 * 邮件服务包
 * 邮件发送服务已经完成，使用apache mail 包
 * 
 * 邮件接收功能尚未完成，因为用户的目的在框架中并不明确，
 * 1，用户可能用此框架制作邮件客户端    保存邮件以及附件   消耗太大
 * 2，有可能用户只是接收邮件并查询其中的内容，根据内容来作出不同的逻辑操作    并不保存邮件 只是解析 
 * 
 * 对于以上两种情况，应该作出不同的对应，因此需要充分利用接口来让用户去实现自己的功能
 * 
 * 简化封装
 */
package org.sxb.plugin.mail;