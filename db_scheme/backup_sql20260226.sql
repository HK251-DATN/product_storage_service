--
-- PostgreSQL database dump
--

\restrict hUGGAlrycNE0963Eh7Eda5njOQbsSREeUjpVUDiKTrmkHzcEbuifxzyIHRYsRo7

-- Dumped from database version 17.8 (Ubuntu 17.8-1.pgdg22.04+1)
-- Dumped by pg_dump version 17.8 (Ubuntu 17.8-1.pgdg22.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: product_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.product_status_enum AS ENUM (
    'STORED',
    'EXPIRED',
    'PICKED',
    'IN_TRANSIT',
    'DELIVERED',
    'RETURNED',
    'DISPOSED'
);


--
-- Name: storage_tool_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.storage_tool_status_enum AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'FULL',
    'IN_MAINTAINANCE'
);


--
-- Name: storage_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.storage_type_enum AS ENUM (
    'FRIDGE',
    'RACK'
);


--
-- Name: unit_cate_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.unit_cate_enum AS ENUM (
    'WEIGHT',
    'VOLUME',
    'COUNT'
);


--
-- Name: unit_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.unit_enum AS ENUM (
    'KILOGRAM',
    'GRAM',
    'PIECE',
    'DOZEN',
    'LITER',
    'MILLILITER',
    'PACK',
    'BOX',
    'BOTTLE'
);


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: frozen_fridges; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.frozen_fridges (
    fridge_id bigint NOT NULL,
    cur_temp bigint,
    min_temp bigint,
    max_temp bigint,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    storage_tool_id bigint NOT NULL
);


--
-- Name: frozen_fridges_fridge_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.frozen_fridges ALTER COLUMN fridge_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.frozen_fridges_fridge_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: product_batchs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_batchs (
    batch_id bigint NOT NULL,
    quantity bigint NOT NULL,
    unit public.unit_enum NOT NULL,
    note character varying(255),
    received_at timestamp without time zone NOT NULL,
    expired_at date NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    provider_id bigint
);


--
-- Name: product_batch_batch_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.product_batchs ALTER COLUMN batch_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.product_batch_batch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: product_details; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_details (
    prod_detail_id bigint NOT NULL,
    status public.product_status_enum DEFAULT 'STORED'::public.product_status_enum NOT NULL,
    price bigint NOT NULL,
    num_of_star integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    storage_tool_id bigint,
    batch_id bigint,
    prod_gen_id bigint,
    unit public.unit_enum,
    unit_quantity bigint
);


--
-- Name: product_details_prod_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.product_details ALTER COLUMN prod_detail_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.product_details_prod_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: product_generals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_generals (
    prod_gen_id bigint NOT NULL,
    name character varying(255),
    updated_at timestamp without time zone,
    created_at timestamp without time zone
);


--
-- Name: rack_levels; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rack_levels (
    rack_level_id bigint NOT NULL,
    usage_percentage bigint,
    rack_id bigint NOT NULL
);


--
-- Name: rack_levels_rack_level_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.rack_levels ALTER COLUMN rack_level_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.rack_levels_rack_level_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: racks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.racks (
    rack_id bigint NOT NULL,
    num_of_level bigint,
    storage_tool_id bigint NOT NULL
);


--
-- Name: rack_rack_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.racks ALTER COLUMN rack_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.rack_rack_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: storage_tools; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.storage_tools (
    storage_tool_id bigint NOT NULL,
    last_maintainance_date date,
    status public.storage_tool_status_enum,
    usage_percentage bigint,
    warehouse_id bigint,
    tool_type public.storage_type_enum NOT NULL
);


--
-- Name: storage_tools_storage_tool_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.storage_tools ALTER COLUMN storage_tool_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.storage_tools_storage_tool_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: warehouses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.warehouses (
    warehouse_id bigint NOT NULL,
    address character varying(255),
    usage_percentage bigint,
    num_of_fridge bigint,
    num_of_rack bigint
);


--
-- Name: warehouses_warehouse_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.warehouses ALTER COLUMN warehouse_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.warehouses_warehouse_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: frozen_fridges; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.frozen_fridges (fridge_id, cur_temp, min_temp, max_temp, storage_tool_id) FROM stdin;
\.


--
-- Data for Name: product_batchs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.product_batchs (batch_id, quantity, unit, note, received_at, expired_at, provider_id) FROM stdin;
\.


--
-- Data for Name: product_details; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.product_details (prod_detail_id, status, price, num_of_star, created_at, updated_at, storage_tool_id, batch_id, prod_gen_id, unit, unit_quantity) FROM stdin;
\.


--
-- Data for Name: product_generals; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.product_generals (prod_gen_id, name, updated_at, created_at) FROM stdin;
\.


--
-- Data for Name: rack_levels; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.rack_levels (rack_level_id, usage_percentage, rack_id) FROM stdin;
\.


--
-- Data for Name: racks; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.racks (rack_id, num_of_level, storage_tool_id) FROM stdin;
\.


--
-- Data for Name: storage_tools; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.storage_tools (storage_tool_id, last_maintainance_date, status, usage_percentage, warehouse_id, tool_type) FROM stdin;
\.


--
-- Data for Name: warehouses; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.warehouses (warehouse_id, address, usage_percentage, num_of_fridge, num_of_rack) FROM stdin;
\.


--
-- Name: frozen_fridges_fridge_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.frozen_fridges_fridge_id_seq', 1, false);


--
-- Name: product_batch_batch_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.product_batch_batch_id_seq', 1, false);


--
-- Name: product_details_prod_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.product_details_prod_detail_id_seq', 1, false);


--
-- Name: rack_levels_rack_level_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.rack_levels_rack_level_id_seq', 1, false);


--
-- Name: rack_rack_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.rack_rack_id_seq', 1, false);


--
-- Name: storage_tools_storage_tool_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.storage_tools_storage_tool_id_seq', 1, false);


--
-- Name: warehouses_warehouse_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.warehouses_warehouse_id_seq', 1, false);


--
-- Name: frozen_fridges frozen_fridges_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.frozen_fridges
    ADD CONSTRAINT frozen_fridges_pkey PRIMARY KEY (fridge_id);


--
-- Name: frozen_fridges frozen_fridges_storage_tool_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.frozen_fridges
    ADD CONSTRAINT frozen_fridges_storage_tool_id_key UNIQUE (storage_tool_id);


--
-- Name: product_batchs product_batch_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_batchs
    ADD CONSTRAINT product_batch_pkey PRIMARY KEY (batch_id);


--
-- Name: product_details product_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_details
    ADD CONSTRAINT product_details_pkey PRIMARY KEY (prod_detail_id);


--
-- Name: product_generals product_generals_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_generals
    ADD CONSTRAINT product_generals_pkey PRIMARY KEY (prod_gen_id);


--
-- Name: rack_levels rack_levels_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rack_levels
    ADD CONSTRAINT rack_levels_pkey PRIMARY KEY (rack_level_id);


--
-- Name: racks rack_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.racks
    ADD CONSTRAINT rack_pkey PRIMARY KEY (rack_id);


--
-- Name: racks rack_storage_tool_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.racks
    ADD CONSTRAINT rack_storage_tool_id_key UNIQUE (storage_tool_id);


--
-- Name: storage_tools storage_tools_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_tools
    ADD CONSTRAINT storage_tools_pkey PRIMARY KEY (storage_tool_id);


--
-- Name: warehouses warehouses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouses
    ADD CONSTRAINT warehouses_pkey PRIMARY KEY (warehouse_id);


--
-- Name: frozen_fridges frozen_fridges_storage_tool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.frozen_fridges
    ADD CONSTRAINT frozen_fridges_storage_tool_id_fkey FOREIGN KEY (storage_tool_id) REFERENCES public.storage_tools(storage_tool_id) NOT VALID;


--
-- Name: product_details product_details_batch_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_details
    ADD CONSTRAINT product_details_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES public.product_batchs(batch_id) NOT VALID;


--
-- Name: product_details product_details_prod_gen_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_details
    ADD CONSTRAINT product_details_prod_gen_id_fkey FOREIGN KEY (prod_gen_id) REFERENCES public.product_generals(prod_gen_id) NOT VALID;


--
-- Name: product_details product_details_storage_tool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_details
    ADD CONSTRAINT product_details_storage_tool_id_fkey FOREIGN KEY (storage_tool_id) REFERENCES public.storage_tools(storage_tool_id) NOT VALID;


--
-- Name: rack_levels rack_levels_rack_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rack_levels
    ADD CONSTRAINT rack_levels_rack_id_fkey FOREIGN KEY (rack_id) REFERENCES public.racks(rack_id) NOT VALID;


--
-- Name: racks racks_storage_tool_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.racks
    ADD CONSTRAINT racks_storage_tool_id_fkey FOREIGN KEY (storage_tool_id) REFERENCES public.storage_tools(storage_tool_id) NOT VALID;


--
-- Name: storage_tools storage_tools_warehouse_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_tools
    ADD CONSTRAINT storage_tools_warehouse_id_fkey FOREIGN KEY (warehouse_id) REFERENCES public.warehouses(warehouse_id) NOT VALID;


--
-- PostgreSQL database dump complete
--

\unrestrict hUGGAlrycNE0963Eh7Eda5njOQbsSREeUjpVUDiKTrmkHzcEbuifxzyIHRYsRo7

