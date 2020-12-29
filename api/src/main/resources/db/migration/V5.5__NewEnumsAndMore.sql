-- support bike trips
ALTER TYPE location_type ADD VALUE IF NOT EXISTS 'BIKE';

-- support bar + food aka restaurants
ALTER TYPE location_type ADD VALUE IF NOT EXISTS 'BARFOOD';
