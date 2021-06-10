// {
//   "filename": "20201015_132245_autobahn_mini.jpg",
//   "presignedUrl": "https://timafe-angkor-data-dev.s3.eu-central-1.amazonaws.com/appdata/places/12345...."
//   "tags": {
//   "fx": "nasen-baer",
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
