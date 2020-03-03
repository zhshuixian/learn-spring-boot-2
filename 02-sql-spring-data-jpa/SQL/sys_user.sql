create table sys_user
(
	user_id bigint auto_increment,
	username varchar(18) not null,
	nickname varchar(36) not null,
	user_age tinyint null,
	user_sex varchar(2) null,
	constraint sys_user_pk
		primary key (user_id)
);