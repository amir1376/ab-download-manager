use suppaftp::AsyncFtpStream;
fn main() {
    let mut control: AsyncFtpStream = unimplemented!();
    let _ = control.resume_transfer(100);
}
