# Generated by Django 4.0.5 on 2023-08-29 21:11

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('services', '0004_fivegsttoken_and_more'),
    ]

    operations = [
        migrations.CreateModel(
            name='IperfMeasurementResult',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('start_timestamp', models.DateTimeField()),
            ],
        ),
        migrations.CreateModel(
            name='IperfProbeResult',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('timestamp', models.DateTimeField()),
                ('speed_bits_per_second', models.IntegerField()),
                ('measurement', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='probes', to='services.iperfmeasurementresult')),
            ],
        ),
    ]
