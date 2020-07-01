import { Component, OnInit } from '@angular/core';
import {LoginService} from '../core/login.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.sass']
})
export class HomeComponent implements OnInit {

  constructor(private loginService: LoginService) { }

  ngOnInit(): void {
  }

  login() {
    this.loginService.login();
  }

}
