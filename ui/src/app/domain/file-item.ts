// {
//   "filename": "20201015_132245_autobahn_mini.jpg",
//   "presignedUrl": "https://timafe-angkor-data-dev.s3.eu-central-1.amazonaws.com/appdata/places/12345/20201015_132245_autobahn_mini.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAJH33Q7CAVTI2GMQA%2F20201111%2Feu-central-1%2Fs3%2Faws4_request&X-Amz-Date=20201111T211236Z&X-Amz-Expires=1800&X-Amz-SignedHeaders=host&X-Amz-Signature=d21dae8f894bbbe5b6ab4d54b8fcbe42ba553caf8adca9972344bbd912ac63f0",
//   "tags": {
//   "fx": "nasenbaer",
//     "horst": "klaus"
// }

export interface FileItem {
  filename: string;
  presignedUrl?: string;
  tags?: any;
}

export interface FileUpload {
  entityType: string;
  entityId: string;
  url: string;
  filename?: string;
}
