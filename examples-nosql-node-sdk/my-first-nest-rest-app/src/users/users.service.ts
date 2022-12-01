import { Injectable } from '@nestjs/common';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { NoSQLService } from '.././nosql.service';


@Injectable()
export class UsersService {
  async create(createUserDto: CreateUserDto) {
    return await NoSQLService.create('users', createUserDto);
  }

  async findAll(params) {
    return await NoSQLService.findAll('users', params);
  }

  async findOne(id: string) {
    return await NoSQLService.findOne('users', id);
  }

  async update(id: string, updateUserDto: UpdateUserDto) {
    return await NoSQLService.update('users', id, updateUserDto);
  }

  async remove(id: string) {
    return await NoSQLService.remove('users', id);
  }
}
