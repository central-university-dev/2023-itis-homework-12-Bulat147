
create table item (
    item_id bigint primary key not null,
    price int,
    name varchar(62),
    itemurl varchar,
    cat varchar,
    type varchar,
    catalogue_id bigint references catalogue(catalogue_id)
);

create table remain(
    item_id bigint references item(item_id),
    region_id int,
    price int
);

create table item_sku (
    sku varchar,
    item_id bigint references item(item_id)
);

create table catalogue (
    catalogue_id bigint primary key not null,
    name varchar,
    parent_id bigint references catalogue(catalogue_id),
    image varchar,
    realcatname varchar
);

