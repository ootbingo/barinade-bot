create table player (
  id            int8 not null,
  racetime_id   varchar(255) null,
  racetime_name varchar(255) null,
  srl_id        int8 null,
  srl_name      varchar(255) null,
  constraint player_pkey primary key (id)
);

create sequence player_seq
  increment by 1
  minvalue 1
  maxvalue 9223372036854775807
  start 1
	CACHE 1
	no cycle;

create table race (
  race_id  varchar(255) not null,
  datetime timestamp(6) null,
  goal     varchar(255) null,
  platform varchar(255) check (platform in ('SRL', 'RACETIME')),
  constraint race_pkey primary key (race_id)
);

create table race_result (
  place       int8         not null,
  result_type varchar(255) check (result_type in ('FINISH', 'FORFEIT', 'DQ')),
  "time"      numeric(21) null,
  player_id   int8         not null,
  race_id     varchar(255) not null,
  constraint race_result_pkey primary key (player_id, race_id),
  constraint fk_result_race foreign key (race_id) references public.race (race_id),
  constraint fk_result_player foreign key (player_id) references public.player (id)
);

create table discord_player (
  player_id int8 not null,
  "name"    varchar(255) null,
  constraint discord_player_pkey primary key (player_id)
);

create table discord_race (
  race_id         int8 not null,
  additional_info text null,
  end_time        timestamptz(6) null,
  "name"          varchar(255) null,
  opening_time    timestamptz(6) null,
  start_time      timestamptz(6) null,
  state           varchar(255) check (state in ('OPEN', 'STARTING', 'PROGRESS', 'FINISHED', 'ABORTED')),
  "type"          varchar(255) check (type in ('GENERIC', 'LOCKOUT')),
  constraint discord_race_pkey primary key (race_id)
);

create table discord_race_entry (
  place     int4 null,
  state     varchar(255) check (state in ('NOT_READY', 'READY', 'PLAYING', 'FINISHED', 'FORFEIT', 'DQ', 'NOT_RANKED')),
  "time"    numeric(21) null,
  race_id   int8 not null,
  player_id int8 not null,
  constraint discord_race_entry_pkey primary key (player_id, race_id),
  constraint fk_discordentry_race foreign key (race_id) references public.discord_race (race_id),
  constraint fk_discordentry_player foreign key (player_id) references public.discord_player (player_id)
);
