--
-- Table structure for table `pictures`
--

create table pictures
(
    sol  int PRIMARY KEY,
    url  varchar(255) not null,
    size bigint       not null
);

--
-- Table structure for table `ip_addresses`
--

create table ip_addresses
(
    ip_address varchar(255) PRIMARY KEY
);

--
-- Table structure for table `picture_ip_address`
--

create table picture_ip_address
(
    sol        int          not null,
    ip_address varchar(255) not null,
    constraint PRIMARY KEY (sol, ip_address),
    constraint FOREIGN KEY (sol) references pictures (sol),
    constraint FOREIGN KEY (ip_address) references ip_addresses (ip_address)
);