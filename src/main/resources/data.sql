insert into client(name) values ('Zing');
insert into account(client_id, name, state) values (1, 'saving', 'ACTIVE');
insert into wallet (balance, asset, account_id) values (10000, 'USD', 1);
insert into wallet (balance, asset, account_id) values (10000, 'EUR', 1);

insert into client(name) values ('Client A');
insert into account(client_id, name, state) values (2, 'investment', 'ACTIVE');
insert into wallet (balance, asset, account_id) values (0, 'USD', 2);
insert into wallet (balance, asset, account_id) values (0, 'EUR', 2);

insert into client(name) values ('Client B');