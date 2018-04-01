using OpenSSL.Core;
using OpenSSL.Crypto;
using OpenSSL.X509;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace BuilderCert
{
    class Program
    {
        static void Main(string[] args)
        {
            X509Name issuer = new X509Name("issuer");

            X509Name subject = new X509Name("subject");
            RSA rsa = new RSA();
            rsa.GenerateKeys(512, 0x10021, null, null);
            CryptoKey key = new CryptoKey(rsa);

            X509Certificate cert = new X509Certificate(123, subject, issuer, key, DateTime.Now,
                                                                  DateTime.Now.AddDays(200));

            File.WriteAllText(@"C:\Users\artik\Desktop\public.txt", rsa.PublicKeyAsPEM);
            File.WriteAllText(@"C:\Users\artik\Desktop\private.txt", rsa.PrivateKeyAsPEM);

            BIO bio = BIO.File("C:/temp/cert.cer", "w");
            cert.Write(bio);
        }
    }
}
