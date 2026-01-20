import { Component, OnInit } from '@angular/core';
import { HelloService } from './hello.service';

@Component({
  selector: 'app-hello',
  standalone: false,
  templateUrl: './hello.html',
  styleUrl: './hello.css',
})
export class Hello implements OnInit {
    message: string = ""; 
    
    constructor(private service: HelloService) {}

    ngOnInit(): void {

        console.log("This works fine!")

        this.service.hello().subscribe({
            next: (response: any) => {
                this.message = response; 
                console.log(`This is the message: ${this.message}`); 
            }, 
            error: (err: any) => {
                console.error("Failed to get the hello message: ", err); 
            }
        })
    }
}
