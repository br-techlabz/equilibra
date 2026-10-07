export interface Tag {
  id: string;
  name: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TagRequest {
  name: string;
}
