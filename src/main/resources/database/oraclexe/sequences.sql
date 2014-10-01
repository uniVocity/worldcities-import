create sequence region_seq 			start with 1 increment by 1 nomaxvalue; 
create sequence city_seq 			start with 1 increment by 1 nomaxvalue; 
create sequence worldcities_seq 	start with 1 increment by 1 nomaxvalue; 
create trigger region_trigger 		before insert on region 		for each row begin select region_seq.nextval 		into :new.id from dual; end; 
create trigger city_trigger 		before insert on city 			for each row begin select city_seq.nextval 			into :new.id from dual; end; 
create trigger worldcities_trigger 	before insert on worldcities 	for each row begin select worldcities_seq.nextval 	into :new.id from dual; end; 
