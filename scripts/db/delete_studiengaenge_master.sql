use `heroku_9852f75c8ae3ea1`;
DELETE 	del,
	    sel
FROM 	`heroku_9852f75c8ae3ea1`.`studiengaenge` as del
JOIN 	`heroku_9852f75c8ae3ea1`.`studiengaenge_attribute` as sel
WHERE del.abschluss like "%master%" and sel.id = del.id
