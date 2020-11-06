create table lar2020_q1 as select * from loanapplicationregister2020 where true = false;
create table lar2020_q2 as select * from loanapplicationregister2020 where true = false;
create table lar2020_q3 as select * from loanapplicationregister2020 where true = false;

alter table lar2020_q1 add column checksum varchar;
alter table lar2020_q2 add column checksum varchar;
alter table lar2020_q3 add column checksum varchar;
