create table user
(
    username varchar(18) not null primary key,
    age tinyint null
);

insert into user value ('spring',1);
insert into user value ('admin',2);
insert into user value ('boot',2);
insert into user value ('github',4);
insert into user value ('git',5);
insert into user value ('chat',6);