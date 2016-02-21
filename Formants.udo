opcode Formants, aa, aa
ain1, ain2 xin

iformants = gi_vox_formants
iamps = gi_vox_amps
ibws = gi_vox_bws
itris = gi_vox_idxs

iminx = 100
imaxx = 900
iminy = 500
imaxy = 2300

kx  = ktrlx;&lt;f0f1X&gt;;loopseg 0.1,0,0,0,1,0.5,2,0;
ky = ktrly;&lt;f0f1Y&gt;;loopseg 0.1,0,0,0.5,1,0,2,0.5;
kf0 = kx * imaxx + (1-kx) * iminx
kf1 = ky * imaxy + (1-ky) * iminy

kidx init 0

ibgx = iminx
ibgy = iminy
ibdx = imaxx
ibdy = iminy
ihdx = imaxx
ihdy = imaxy
ihgx = iminx
ihgy = imaxy

;
;tessitures : &quot;0:Bass            1:Tenor        2:CTenor          3:Alto          4:Soprano&quot;
;vowels :     &quot;0:eu      1:a         2:e         3:o       4:i                   5:u&quot;
;

itess = 2

ka table kidx, itris
kb table kidx+1, itris
kc table kidx+2, itris
if (ka<6) then
  kx0 table 5*(6*itess+ka),iformants
  ky0 table 5*(6*itess+ka)+1,iformants
else
  kx0 = (ka==6?ibgx:(ka==7?ibdx:(ka==8?ihdx:ihgx)))
  ky0 = (ka==6?ibgy:(ka==7?ibdy:(ka==8?ihdy:ihgy)))
endif
if (kb<6) then
  kx1 table 5*(6*itess+kb),iformants
  ky1 table 5*(6*itess+kb)+1,iformants
else 
  kx1 = (kb==6?ibgx:(kb==7?ibdx:(kb==8?ihdx:ihgx)))
  ky1 = (kb==6?ibgy:(kb==7?ibdy:(kb==8?ihdy:ihgy)))
endif
if (kc<6) then
  kx2 table 5*(6*itess+kc),iformants
  ky2 table 5*(6*itess+kc)+1,iformants
else 
  kx2 = (kc==6?ibgx:(kc==7?ibdx:(kc==8?ihdx:ihgx)))
  ky2 = (kc==6?ibgy:(kc==7?ibdy:(kc==8?ihdy:ihgy)))
endif
k2s2 = kf0*(ky0-ky1) + kx0*(ky1-kf1) + kx1*(kf1-ky0)
k2s0 = kf0*(ky1-ky2) + kx1*(ky2-kf1) + kx2*(kf1-ky1)
k2s1 = kf0*(ky2-ky0) + kx2*(ky0-kf1) + kx0*(kf1-ky2)
k2s = k2s0 + k2s1 + k2s2
if (k2s0>0 || k2s1>0 || k2s2>0) then
 kidx = kidx + 3
 if (kidx >= 36) then
   kidx = kidx - 36
 endif
endif

ktrig 	changed	 kx,ky,kidx
if (ktrig==1 && k2s0>=0 && k2s1>=0 && k2s2>=0) then
  kl0 =  k2s0/k2s
  kl1 =  k2s1/k2s
  kl2 =  k2s2/k2s

  ka0 voxinterpol iamps, itess, 0, ka, kb, kc, kl0, kl1, kl2
  kbw0 voxinterpol ibws, itess, 0, ka, kb, kc, kl0, kl1, kl2
  ka1 voxinterpol iamps, itess, 1, ka, kb, kc, kl0, kl1, kl2
  kbw1 voxinterpol ibws, itess, 1, ka, kb, kc, kl0, kl1, kl2
  kf2 voxinterpol iformants, itess, 2, ka, kb, kc, kl0, kl1, kl2
  ka2 voxinterpol iamps, itess, 2, ka, kb, kc, kl0, kl1, kl2
  kbw2 voxinterpol ibws, itess, 2, ka, kb, kc, kl0, kl1, kl2
  kf3 voxinterpol iformants, itess, 3, ka, kb, kc, kl0, kl1, kl2
  ka3 voxinterpol iamps, itess, 3, ka, kb, kc, kl0, kl1, kl2
  kbw3 voxinterpol ibws, itess, 3, ka, kb, kc, kl0, kl1, kl2
  kf4 voxinterpol iformants, itess, 4, ka, kb, kc, kl0, kl1, kl2
  ka4 voxinterpol iamps, itess, 4, ka, kb, kc, kl0, kl1, kl2
  kbw4 voxinterpol ibws, itess, 4, ka, kb, kc, kl0, kl1, kl2
endif

kfreq = cpspch(p4)
a0left butterbp ain1, kf0, kbw0
a1left butterbp ain1, kf1, kbw1
a2left butterbp ain1, kf2, kbw2
a3left butterbp ain1, kf3, kbw3
a4left butterbp ain1, kf4, kbw4

a0right butterbp ain2, kf0, kbw0
a1right butterbp ain2, kf1, kbw1
a2right butterbp ain2, kf2, kbw2
a3right butterbp ain2, kf3, kbw3
a4right butterbp ain2, kf4, kbw4

aleft = a0left*ampdbfs(ka0)+a1left*ampdbfs(ka1)+a2left*ampdbfs(ka2)+a3left*ampdbfs(ka3)+a4left*ampdbfs(ka4) ; Scale and sum
aright = a0right*ampdbfs(ka0)+a1right*ampdbfs(ka1)+a2right*ampdbfs(ka2)+a3right*ampdbfs(ka3)+a4right*ampdbfs(ka4) ; Scale and sum

adeclick linsegr 0,0.01,1,0.01,0
iwet = 0.8
kwet = sqrt(iwet)
aout1 = adeclick*((1-kwet)*ain1+kwet*aleft)
aout2 = adeclick*((1-kwet)*ain2+kwet*aright)
xout aout1, aout2
endop
