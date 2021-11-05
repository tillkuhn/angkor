import {EntityType} from '@shared/domain/entities';
import {ApiHelper} from '@shared/helpers/api-helper';

describe('ApiHelper', () => {


  it('should return valid url path for all Entity Types', () => {
    for (const enumKey of Object.keys(EntityType)) {
      const enu = EntityType[enumKey];
      expect(enumKey).toBeTruthy();
      const apiUrl = ApiHelper.getApiUrl(enu);
      expect(apiUrl).toBe('/api/v1/' + ApiHelper.getApiPath(enu));
    }

  });

});
