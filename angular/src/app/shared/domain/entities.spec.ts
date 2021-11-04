import {EntityMetadataLookup, EntityType} from '@shared/domain/entities';

describe('EntityMetadata', () => {


  it('create an instance', () => {
    for (const enumKey of Object.keys(EntityType)) {
      expect(enumKey).toBeTruthy();
      expect(typeof enumKey).toBe('string');

      const eMeta = EntityMetadataLookup[EntityType[enumKey]];
      expect(eMeta).toBeTruthy();
      expect(eMeta.icon).toBe(enumKey.toLowerCase());
    }
    const um = EntityMetadataLookup.USER;
    expect(um).toBeTruthy();
    expect(um.iconUrl).toEqual('/assets/icons/user.svg');
    expect(um.name).toEqual('User');

    // console.log(EntityLookup);
  });

  it('should allow lookup by enum', () => {
    const pm = EntityMetadataLookup[EntityType.Place];
    expect(pm).toBeTruthy();
    expect(pm.name).toEqual('Place');
  });

  });
