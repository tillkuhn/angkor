-- Values in etype discriminator column should be consistent with new Enum Names (UpperCammelCase)

UPDATE location SET etype='Place' where etype='PLACE';
UPDATE location SET etype='Post' where etype='POST';
UPDATE location SET etype='Tour' where etype='TOUR';
UPDATE location SET etype='Video' where etype='VIDEO';

-- Since discriminator column has to be a String for JPA (not an enum),
-- restrict possible values with this contstraint
ALTER TABLE location ADD CONSTRAINT check_etypes CHECK (etype IN ('Dish','Place','Photo','Post','Tour','Video'));
