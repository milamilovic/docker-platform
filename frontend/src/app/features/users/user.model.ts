export interface UserDto {
  id?: number;
  email: string;
  username: string;
  password?: string;
  badge?: string;
}

export interface UserInfo {
  email: string;
  username: string;
}
